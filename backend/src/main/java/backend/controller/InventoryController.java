package backend.controller;

import backend.exception.InventoryNotFoundException;
import backend.model.InventoryModel;
import backend.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map; // Import for Map

@RestController
@CrossOrigin("http://localhost:3000")
public class InventoryController {
    @Autowired
    private InventoryRepository inventoryRepository;

    // --- EXISTING CODE ---

    //Normal Data insert
    @PostMapping("/inventory")
    public InventoryModel newInventoryModel(@RequestBody InventoryModel newInventoryModel) {
        return inventoryRepository.save(newInventoryModel);
    }

    //Image Insert
    @PostMapping("/inventory/itemImg")
    public String itemImg(@RequestParam("file")MultipartFile file) {
        String folder = "src/main/uploads/";
        String itemImage = file.getOriginalFilename();

        try{
            File uploadDir = new File(folder);

            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            file.transferTo(Paths.get(folder+itemImage));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error uploading file" +itemImage;
        }
        return itemImage;
    }

    //Display all
    @GetMapping("/inventory")
    List<InventoryModel> getAllInventory() {
        return inventoryRepository.findAll();
    }

    //Display using id
    @GetMapping("/inventory/{id}")
    InventoryModel getInventory(@PathVariable Long id) {
        return inventoryRepository.findById(id).orElseThrow(()-> new InventoryNotFoundException(id));
    }

    //Display Image
    private final String UPLOAD_DIR = "src/main/uploads/";
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String filename) {
        File file = new File(UPLOAD_DIR+filename);
        if(!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new FileSystemResource(file));
    }

    // --- NEW / MODIFIED CODE ---

    // Original Update endpoint (for full item updates, including optional file)
    // This expects FormData with "itemDetails" and optionally "file"
    @PutMapping("/inventory/{id}")
    public InventoryModel updateItem(
            @RequestPart(value = "itemDetails") String itemDetails,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @PathVariable Long id
    ) {
        System.out.println("Item Details (full update): " + itemDetails);
        if(file!=null){
            System.out.println("File received: " + file.getOriginalFilename());
        } else {
            System.out.println("No file Uploaded for full update");
        }
        ObjectMapper mapper = new ObjectMapper();
        InventoryModel newInventory;
        try {
            newInventory = mapper.readValue(itemDetails, InventoryModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing itemDetails for full update", e);
        }
        return inventoryRepository.findById(id).map(existingInventory -> {
            // Only update fields that are explicitly provided and make sense for an update
            // Do NOT set itemId from newInventory, as the ID should be fixed.
            // existingInventory.setItemId(newInventory.getItemId()); // REMOVE THIS LINE IF ID IS AUTO-GENERATED/FIXED
            if (newInventory.getItemName() != null) {
                existingInventory.setItemName(newInventory.getItemName());
            }
            if (newInventory.getItemCategory() != null) {
                existingInventory.setItemCategory(newInventory.getItemCategory());
            }
            // Be careful if itemQty is meant to be updated here too, typically this is for ALL fields
            existingInventory.setItemQty(newInventory.getItemQty()); // This assumes itemQty is always part of itemDetails
            if (newInventory.getItemDetails() != null) {
                existingInventory.setItemDetails(newInventory.getItemDetails());
            }


            if(file!=null && !file.isEmpty()) {
                String folder = "src/main/uploads/";
                String itemImage = file.getOriginalFilename();
                try {
                    // Consider deleting old image if a new one is uploaded
                    file.transferTo(Paths.get(folder+itemImage));
                    existingInventory.setItemImage(itemImage);
                } catch (IOException e) {
                    throw new RuntimeException("Error saving uploaded file for full update", e);
                }
            }
            return inventoryRepository.save(existingInventory);
        }).orElseThrow(()-> new InventoryNotFoundException(id));
    }

    // NEW Endpoint for updating only item quantity
    @PutMapping("/inventory/quantity/{id}")
    public InventoryModel updateItemQuantity(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload
    ) {
        Integer itemQty = payload.get("itemQty");
        System.out.println("PUT /inventory/quantity/" + id + " with itemQty: " + itemQty);

        if (itemQty == null) {
            throw new IllegalArgumentException("itemQty is required in the request body.");
        }

        return inventoryRepository.findById(id).map(existingInventory -> {
            System.out.println("Found inventory: " + existingInventory);
            existingInventory.setItemQty(itemQty);
            InventoryModel updated = inventoryRepository.save(existingInventory);
            System.out.println("Saved inventory with new quantity: " + updated.getItemQty());
            return updated;
        }).orElseThrow(() -> new InventoryNotFoundException(id));
    }


    //Delete
    @DeleteMapping("/inventory/{id}")
    String deleteItem(@PathVariable Long id) {
        //Check item is exists db
        InventoryModel inventoryItem = inventoryRepository.findById(id)
                .orElseThrow(()-> new InventoryNotFoundException(id));

        //Delete image part
        String itemImage = inventoryItem.getItemImage();
        if(itemImage!=null && !itemImage.isEmpty()) {
            File imageFile = new File("src/main/uploads/"+itemImage);
            if(imageFile.exists()) {
                if(imageFile.delete()) {
                    System.out.println("Image file deleted successfully for item " + id);
                } else {
                    System.out.println("Failed to delete image file for item " + id);
                }
            }
        }
        //Delete item from the repo
        inventoryRepository.deleteById(id);
        return "data with id " + id + " and image deleted";
    }
}