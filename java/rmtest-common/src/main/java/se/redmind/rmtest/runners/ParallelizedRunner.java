package se.redmind.rmtest.runners;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * @author Johan Grimlund
 */
public class ParallelizedRunner extends BlockJUnit4ClassRunner implements Parallelizable {

    public ParallelizedRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        parallelize();
    }

}
