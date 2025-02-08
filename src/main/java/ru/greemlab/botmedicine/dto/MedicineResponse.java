package ru.greemlab.botmedicine.dto;

import java.util.List;

public record MedicineResponse(
        Embedded _embedded,
        PageLinks _links,
        Page page
) {
    public record Embedded(
            List<MedicineViewList> medicineViewList
    ) {}

    public record MedicineViewList(
            int id,
            String name,
            String serialNumber,
            String expirationDate,
            String color
    ) {}

    public record PageLinks(
            Link first,
            Link self,
            Link next,
            Link last
    ) {}

    public record Link(
            String href
    ) {}

    public record Page(
            int size,
            int totalElements,
            int totalPages,
            int number
    ) {}
}
