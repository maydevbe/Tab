package io.github.maydevbe.layout;

import io.github.maydevbe.reflect.TabReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TabLayout {

    private UUID id;
    private String name;

    private int tabSlot;
    private int ping;

    private String textLine;
    private String[] skinArray = TabReflection.DARK_GRAY_SKIN_ARRAY;

    public boolean equalsSkinArray(String[] skinArray) {
        // Verificar si this.skinArray y skinArray no son null
        if (this.skinArray == null || skinArray == null) {
            return false;
        }

        // Verificar si this.skinArray y skinArray tienen al menos dos elementos
        if (this.skinArray.length < 2 || skinArray.length < 2) {
            return false;
        }

        // Realizar la comparación ignorando mayúsculas y minúsculas
        return this.skinArray[0].equalsIgnoreCase(skinArray[0]) && this.skinArray[1].equalsIgnoreCase(skinArray[1]);
    }
}
