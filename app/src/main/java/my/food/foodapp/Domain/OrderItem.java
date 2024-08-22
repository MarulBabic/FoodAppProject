package my.food.foodapp.Domain;

import java.util.List;

public class OrderItem {

        private Long productId;
        private Integer quantity;
        private double price;
        private String imagePath;
        private String title;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getImagePath(){
            return imagePath;
        }

         public void setImagePath(String imagePath) {
             this.imagePath = imagePath;
         }

        public void setImagePathFromFoods(List<Foods> foodsList) {
            for (Foods food : foodsList) {
                if (food.getId().equals(this.productId)) {
                this.imagePath = food.getImagePath();
                break;  // Prekidamo petlju kada nađemo odgovarajući Foods objekt
                }
             }
        }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
