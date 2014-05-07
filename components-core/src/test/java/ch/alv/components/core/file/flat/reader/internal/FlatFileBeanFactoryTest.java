package ch.alv.components.core.file.flat.reader.internal;

import ch.alv.components.core.file.flat.reader.FlatFileConverterException;
import ch.alv.components.core.mock.BeanA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;

/**
 * Test for the {@link ch.alv.components.core.file.flat.reader.internal.FlatFileBeanFactory}.
 *
 * @since 1.0.0
 */
public class FlatFileBeanFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testSuccess() {
        assertNotNull(new FlatFileBeanFactory().createBean(BeanA.class));
    }

    @Test
    public void testException() {
        exception.expect(FlatFileConverterException.class);
        new FlatFileBeanFactory().createBean(null);
    }

}
