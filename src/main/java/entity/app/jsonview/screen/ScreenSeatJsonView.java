package entity.app.jsonview.screen;

/**
 * Created by mi on 2/15/17.
 */
public class ScreenSeatJsonView {

    public interface Basic {}
    public interface Summary extends Basic,ScreenSeatTypeJsonView.Basic{}
    public interface Details extends  Summary{}
}
