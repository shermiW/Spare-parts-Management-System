package backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class InventoryModel {
    @Id
    @GeneratedValue
    private Long id;
    private String itemId;
    private String itemImage;
    private String itemName;
    private String itemCategory;
    private int itemQty; // <--- CHANGE THIS FROM String to int
    private String itemDetails;

    public InventoryModel() {

    }

    // Update constructor to accept int for itemQty
    public InventoryModel(Long id, String itemId, String itemImage, String itemName, String itemCategory, int itemQty, String itemDetails) {
        this.id = id;
        this.itemId = itemId;
        this.itemImage = itemImage;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.itemQty = itemQty; // Now assigns an int to an int field
        this.itemDetails = itemDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    // Update getter to return int
    public int getItemQty() { // <--- CHANGE THIS FROM String to int
        return itemQty;
    }

    // Update setter to accept int
    public void setItemQty(int itemQty) { // <--- CHANGE THIS FROM String to int
        this.itemQty = itemQty;
    }

    public String getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(String itemDetails) {
        this.itemDetails = itemDetails;
    }
}