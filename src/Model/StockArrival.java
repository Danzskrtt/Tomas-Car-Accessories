package Model;

public class StockArrival {
    private int arrivalId;
    private int productId;
    private String productName;
    private String arrivalDate;
    private int arrivedQty;
    private int defectiveQty;
    private int goodQty;
    private String status;
    private String referenceNumber;

    public StockArrival(int arrivalId, int productId, String productName, String arrivalDate, int arrivedQty, int defectiveQty, int goodQty, String status, String referenceNumber) {
        this.arrivalId = arrivalId;
        this.productId = productId;
        this.productName = productName;
        this.arrivalDate = arrivalDate;
        this.arrivedQty = arrivedQty;
        this.defectiveQty = defectiveQty;
        this.goodQty = goodQty;
        this.status = status;
        this.referenceNumber = referenceNumber;
    }

    public int getArrivalId() { return arrivalId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getArrivalDate() { return arrivalDate; }
    public int getArrivedQty() { return arrivedQty; }
    public int getDefectiveQty() { return defectiveQty; }
    public int getGoodQty() { return goodQty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReferenceNumber() { return referenceNumber; }
}