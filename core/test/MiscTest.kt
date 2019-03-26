import de.sscholz.util.GdxUtil
import de.sscholz.util.printl
import org.junit.Test


class MiscTest {

    @Test
    fun `polygon corner creation`() {
        GdxUtil.createRegularPolygonCorners(4, 2f).printl()
    }
}