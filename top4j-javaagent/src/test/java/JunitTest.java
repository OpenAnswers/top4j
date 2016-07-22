import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JunitTest {
    @Test
    public void testJunit() {
        String str= "Junit is working fine";
        assertEquals("Junit is working fine",str);
        System.out.println("JUnit test complete.");
    }
}
