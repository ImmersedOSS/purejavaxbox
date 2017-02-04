package purejavaxbox.stickapp;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import purejavaxbox.XboxButton;
import purejavaxbox.XboxController;
import purejavaxbox.XboxControllers;
import reactor.core.publisher.Flux;

import java.util.Map;

import static purejavaxbox.xinput.XInputConstants.*;

public class StickAppController
{
    private static final double SCALE_FACTOR = 0.9;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Circle deadZoneCircle, stickCircle;

    @FXML
    private Canvas canvas;

    @FXML
    private Label horizontalLabel, verticalLabel;

    private double deadZone = 0.1;

    private XboxController controller = XboxControllers
            .useDefaults()
            .getController(0);

    @FXML
    public void initialize()
    {
        setDeadZone(LEFT_DZ);
        adjustSizes();
        registerController();
    }

    private void adjustSizes()
    {
        Flux<Number> width = Flux.<Number>create(emitter -> {
            rootPane
                    .widthProperty()
                    .addListener((obs, old, n) -> emitter.next(n));
        }).map(d -> d.doubleValue() * SCALE_FACTOR);

        Flux<Number> height = Flux.<Number>create(emitter -> {
            rootPane
                    .heightProperty()
                    .addListener((obs, old, n) -> emitter.next(n));
        }).map(d -> d.doubleValue() * SCALE_FACTOR);

        width
                .withLatestFrom(height, (x, y) -> Math.min(x.doubleValue(), y.doubleValue()))
                .subscribe(d -> {
                    stickCircle.setRadius(d / 2.0);
                    deadZoneCircle.setRadius(d / 2.0 * deadZone);
                });

        canvas
                .widthProperty()
                .bind(rootPane.widthProperty());
        canvas
                .heightProperty()
                .bind(rootPane.heightProperty());
    }

    private void registerController()
    {
        AnimationTimer timer = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                Map<XboxButton, Number> buttons = controller.buttons();

                double x = buttons
                        .getOrDefault(XboxButton.LEFT_STICK_HORIZONTAL, 0.0)
                        .doubleValue();
                double y = buttons
                        .getOrDefault(XboxButton.LEFT_STICK_VERTICAL, 0.0)
                        .doubleValue();

                horizontalLabel.setText(String.format("%.2f", x));
                verticalLabel.setText(String.format("%.2f", y));

                double w = canvas.getWidth();
                double h = canvas.getHeight();
                GraphicsContext ctx = canvas.getGraphicsContext2D();

                ctx.clearRect(0, 0, w, h);
                ctx.setFill(Color.AQUAMARINE);

                double centerX = w / 2.0;
                double centerY = h / 2.0;
                double stickRadius = stickCircle.getRadius();

                double radius = 5.0;

                double cx = centerX + stickRadius * x;
                double cy = centerY - stickRadius * y;

                ctx.fillOval(cx - radius, cy - radius, radius * 2.0, radius * 2.0);
            }
        };
        timer.start();
    }

    private void setDeadZone(short value)
    {
        deadZone = value / (double) Short.MAX_VALUE;
    }
}
