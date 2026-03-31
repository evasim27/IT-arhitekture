package si.pricescout.stores.dto;

import jakarta.validation.constraints.NotBlank;

public class StoreRequest {

    @NotBlank(message = "Store name must not be blank")
    private String name;

    public StoreRequest() {
    }

    public StoreRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
