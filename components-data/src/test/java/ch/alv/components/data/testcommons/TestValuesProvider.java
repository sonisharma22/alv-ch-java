package ch.alv.components.data.testcommons;


import ch.alv.components.core.search.ValuesProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ValuesProvider implementation for testing purposes.
 *
 * @since 1.0.0
 */
public class TestValuesProvider implements ValuesProvider {
    @Override
    public Map<String, Object> getValues() {
        Map<String, Object> values;
        values = new HashMap<>();
        values.put("aKey", "testKey");
        values.put("aValue", 6);
        values.put("aList", new ArrayList<>());
        return values;
    }
}