package org.easytech.pelatologio.helper;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyEvent;
import org.easytech.pelatologio.models.Accountant;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Item;
import org.easytech.pelatologio.models.Supplier;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ComboBoxHelper {
    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();

    static {
        ENGLISH_TO_GREEK.put('A', 'Α');
        ENGLISH_TO_GREEK.put('B', 'Β');
        ENGLISH_TO_GREEK.put('C', 'Ψ');
        ENGLISH_TO_GREEK.put('D', 'Δ');
        ENGLISH_TO_GREEK.put('E', 'Ε');
        ENGLISH_TO_GREEK.put('F', 'Φ');
        ENGLISH_TO_GREEK.put('G', 'Γ');
        ENGLISH_TO_GREEK.put('H', 'Η');
        ENGLISH_TO_GREEK.put('I', 'Ι');
        ENGLISH_TO_GREEK.put('J', 'Ξ');
        ENGLISH_TO_GREEK.put('K', 'Κ');
        ENGLISH_TO_GREEK.put('L', 'Λ');
        ENGLISH_TO_GREEK.put('M', 'Μ');
        ENGLISH_TO_GREEK.put('N', 'Ν');
        ENGLISH_TO_GREEK.put('O', 'Ο');
        ENGLISH_TO_GREEK.put('P', 'Π');
        ENGLISH_TO_GREEK.put('Q', 'Κ');
        ENGLISH_TO_GREEK.put('R', 'Ρ');
        ENGLISH_TO_GREEK.put('S', 'Σ');
        ENGLISH_TO_GREEK.put('T', 'Τ');
        ENGLISH_TO_GREEK.put('U', 'Θ');
        ENGLISH_TO_GREEK.put('V', 'Ω');
        ENGLISH_TO_GREEK.put('W', 'W');
        ENGLISH_TO_GREEK.put('X', 'Χ');
        ENGLISH_TO_GREEK.put('Y', 'Υ');
        ENGLISH_TO_GREEK.put('Z', 'Ζ');
    }

    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
        GREEK_TO_ENGLISH.put('Α', 'A');
        GREEK_TO_ENGLISH.put('Β', 'B');
        GREEK_TO_ENGLISH.put('Ψ', 'C');
        GREEK_TO_ENGLISH.put('Δ', 'D');
        GREEK_TO_ENGLISH.put('Ε', 'E');
        GREEK_TO_ENGLISH.put('Φ', 'F');
        GREEK_TO_ENGLISH.put('Γ', 'G');
        GREEK_TO_ENGLISH.put('Η', 'H');
        GREEK_TO_ENGLISH.put('Ι', 'I');
        GREEK_TO_ENGLISH.put('Ξ', 'J');
        GREEK_TO_ENGLISH.put('Κ', 'K');
        GREEK_TO_ENGLISH.put('Λ', 'L');
        GREEK_TO_ENGLISH.put('Μ', 'M');
        GREEK_TO_ENGLISH.put('Ν', 'N');
        GREEK_TO_ENGLISH.put('Ο', 'O');
        GREEK_TO_ENGLISH.put('Π', 'P');
        GREEK_TO_ENGLISH.put('Ρ', 'R');
        GREEK_TO_ENGLISH.put('Σ', 'S');
        GREEK_TO_ENGLISH.put('Τ', 'T');
        GREEK_TO_ENGLISH.put('Θ', 'U');
        GREEK_TO_ENGLISH.put('Ω', 'V');
        GREEK_TO_ENGLISH.put('Χ', 'X');
        GREEK_TO_ENGLISH.put('Υ', 'Y');
        GREEK_TO_ENGLISH.put('Ζ', 'Z');
    }

    public static <T> void setupFilter(ComboBox<T> comboBox, FilteredList<T> filteredList) {
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setPrefWidth(comboBox.getWidth());
                    setWrapText(true);
                }
            }
        });

        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toUpperCase();
            filteredList.setPredicate(item -> {
                if (item == null) {
                    return false;
                }
                if (filterText.isEmpty()) {
                    return true;
                }

                char[] chars1 = filterText.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = filterText.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);

                for (Field field : item.getClass().getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(item);
                        if (value != null && (value.toString().toUpperCase().contains(search1) || value.toString().toUpperCase().contains(search2))) {
                            return true;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            });
        });
    }
}