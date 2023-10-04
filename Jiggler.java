import java.awt.*;
import java.util.Random;
import java.util.concurrent.*;
import javax.swing.JFrame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
interface Motion {

  Point nextPos();
}

class PendulumMotion implements Motion {

  private final Point center;
  private final Point extLeft;
  private final Point extRight;
  private final Point currPos;
  private boolean isInMotion;
  private boolean isMovingLeft;
  private final boolean startLeft;
  private final int dx;

  public PendulumMotion(Point center, int dx, boolean startLeft) {
    this.center = center;
    this.startLeft = startLeft;
    this.dx = dx;
    currPos = (Point) center.clone();
    extLeft = new Point(center.x - dx, center.y);
    extRight = new Point(center.x + dx, center.y);

    //    System.out.println(String.format("center: (%d, %d)", currPos.x, currPos.y));
    //    System.out.println(String.format("extLeft: (%d, %d)", extLeft.x, extLeft.y));
    //    System.out.println(String.format("extRight: (%d, %d)", extRight.x, extRight.y));
  }

  public Point nextPos() {
    if (isInMotion) {

      if (isMovingLeft) {

        currPos.setLocation(currPos.x - dx, currPos.y);

        //        System.out.print(String.format("(%d, %d) ", currPos.x, currPos.y));

        if (currPos.equals(extLeft)) {

          isMovingLeft = false;

          //          System.out.println("\ntravelling right");
        }

      } else {

        currPos.setLocation(currPos.x + dx, currPos.y);

//        System.out.print(String.format("(%d, %d) ", currPos.x, currPos.y));

        if (currPos.equals(extRight)) {

          isMovingLeft = true;

          //          System.out.println("\ntravelling left");
        }
      }

    } else {

      initMotion();
    }

    return (Point) currPos.clone();
  }

  private void initMotion() {
    isInMotion = true;

    isMovingLeft = !startLeft;

    if (startLeft) {

      currPos.setLocation(extLeft);

    } else {

      currPos.setLocation(extRight);
    }
  }
}

public class Jiggler {

  private final Random random;

  public Jiggler() {

    random = new Random();
  }

  protected int inRangeRandom(final int i, final int j) {
    final int n = Math.abs(i - j);

    return i + random.nextInt(n);
  }

  private Point currMouseLocation() {

    return MouseInfo.getPointerInfo().getLocation();
  }

  public void startJiggling() {
    final Point startingPoint = MouseInfo.getPointerInfo().getLocation();

    final Motion motion = new PendulumMotion(startingPoint, 1, true);

    try {

      final Robot robot = new Robot();

      Point nextPos, prevPos = startingPoint;

      while (true) {

        nextPos = motion.nextPos();

        if (prevPos.equals(currMouseLocation())) {

          robot.mouseMove(nextPos.x, nextPos.y);

        } else {

          break;
        }

        prevPos = nextPos;

        Thread.sleep(1000);
      }

    } catch (AWTException | InterruptedException ignored) {

    }

    if (detect() != null) {

      startJiggling();
    }
  }

  private Point detect() {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    final Future<Point> stablePointer = executorService.submit(new MouseIsStableObserver());

    try {

      return stablePointer.get();

    } catch (InterruptedException | ExecutionException ignored) {

    }

    return null;
  }

  private Point mouseMoveObserve() {
    Point currPos, prevPos = null;

    while (true) {
      try {
        currPos = currMouseLocation();

        if (currPos.equals(prevPos)) {

          return currPos;
        }

        Thread.sleep(10000);

        prevPos = currPos;

      } catch (InterruptedException ignored) {

      }
    }
  }

  private void observeKeyboardShortcut(KeyEvent e) {
    int keyCode = e.getKeyCode();
  }

  class MouseIsStableObserver implements Callable<Point> {

    @Override
    public Point call() throws Exception {

      return mouseMoveObserve();
    }
  }

  public static void main(String[] args) {
    Jiggler jiggler = new Jiggler();

    jiggler.startJiggling();
  }
}
