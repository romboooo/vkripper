package org.example.entity;

import java.util.Arrays;

public enum ProductGroup {
    WARDROBE("Гардероб"),
    HOME_AND_DACHA("Дом и дача"),
    BEAUTY("Красота"),
    FOR_CHILDREN("Для детей"),
    ELECTRONICS("Электроника"),
    SPORT_AND_LEISURE("Спорт и отдых"),
    FOR_REPAIR("Для ремонта"),
    HOBBY("Хобби"),
    TRANSPORT("Транспорт");
    private final String displayName;

    ProductGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return this.displayName;
    }
    public static ProductGroup fromDisplayName(String displayName){
        return Arrays.stream(values())
                .filter(group -> group.getDisplayName().equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестная группа товаров" + displayName));
    }
}
