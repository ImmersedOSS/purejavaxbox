package purejavaxbox.stickapp;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import purejavaxbox.XboxButton;
import purejavaxbox.api.ControllerApi;
import purejavaxbox.api.ControllerBuilder;
import purejavaxbox.api.StickDeadZones;
import purejavaxbox.raw.XboxControllerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ServiceLoader;

import static purejavaxbox.raw.xinput.XInputConstants.*;

public class StickAppController
{
    private static final double SCALE_FACTOR = 0.9;
    private Scheduler FX_THREAD = Schedulers.fromExecutor(Platform::runLater);

    @FXML
    private BorderPane rootPane;

    @FXML
    private Circle deadZoneCircle, stickCircle;

    @FXML
    private Canvas canvas;

    @FXML
    private Label horizontalLabel, verticalLabel;

    private XboxButton stickX = XboxButton.LEFT_STICK_HORIZONTAL;
    private XboxButton stickY = XboxButton.LEFT_STICK_VERTICAL;

    private volatile Tuple2<Number, Number> rawValue;
    private StickHistory history;

    private Duration duration = Duration.ofSeconds(1L);

    @FXML
    public void initialize()
    {
        adjustSizes();
        registerController();
    }

    private void adjustSizes()
    {
        double deadZone = LEFT_DZ / (double) Short.MAX_VALUE;

        Flux<Number> width = Flux.<Number>create(emitter ->
        {
            rootPane
                    .widthProperty()
                    .addListener((obs, old, n) -> emitter.next(n));
        }).map(d -> d.doubleValue() * SCALE_FACTOR);

        Flux<Number> height = Flux.<Number>create(emitter ->
        {
            rootPane
                    .heightProperty()
                    .addListener((obs, old, n) -> emitter.next(n));
        }).map(d -> d.doubleValue() * SCALE_FACTOR);

        Flux
                .combineLatest(width, height, (x, y) -> Math.min(x.doubleValue(), y.doubleValue()))
                .subscribe(d ->
                {
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
        StickDeadZones stickDeadZones = new StickDeadZones()
                .innerDeadZone(LEFT_DZ)
                .outerDeadZone(Short.MAX_VALUE);

        ControllerApi controllerRaw = new ControllerBuilder().player1();

        Flux<Number> x = controllerRaw.observe(stickX);
        Flux<Number> y = controllerRaw.observe(stickY);
        Flux
                .zip(x, y)
                .subscribe(t -> rawValue = t);

        ControllerApi controllerWithDZ = new ControllerBuilder()
                .mappers(stickDeadZones.usingScaledRadialDeadZone())
                .player1();
        history = new StickHistory(duration, controllerWithDZ, stickX, stickY);

        drawStickPositions();
        updateText(controllerWithDZ, stickX, horizontalLabel);
        updateText(controllerWithDZ, stickY, verticalLabel);
    }

    private void drawStickPositions()
    {
        AnimationTimer timer = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                GraphicsContext ctx = canvas.getGraphicsContext2D();

                double w = canvas.getWidth();
                double h = canvas.getHeight();
                ctx.clearRect(0, 0, w, h);

                ctx.setFill(Color.BLACK);
                ctx.setGlobalAlpha(1.0);

                Tuple2<Number, Number> raw = rawValue;
                drawPoint(raw.getT1(), raw.getT2());

                long currentTime = System.currentTimeMillis();
                long target = duration.toMillis();

                ctx.setFill(Color.AQUAMARINE);
                history.forEach(t ->
                {
                    double diff = (double) (currentTime - t.getT1()) / target;
                    diff = Math.min(1.0, diff);
                    diff = 1.0 - diff;

                    ctx.setGlobalAlpha(diff);
                    drawPoint(t.getT2(), t.getT3());
                });
            }
        };
        timer.start();
    }

    private void drawPoint(Number xNum, Number yNum)
    {
        double x = xNum.doubleValue();
        double y = yNum.doubleValue();

        GraphicsContext ctx = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double centerX = w / 2.0;
        double centerY = h / 2.0;
        double stickRadius = stickCircle.getRadius();
        double radius = 5.0;

        double cx = centerX + stickRadius * x;
        double cy = centerY - stickRadius * y;

        ctx.fillOval(cx - radius, cy - radius, radius * 2.0, radius * 2.0);
    }

    private void updateText(ControllerApi controller, XboxButton button, Label label)
    {
        controller
                .observe(button)
                .publishOn(FX_THREAD)
                .map(n -> String.format("%.2f", n))
                .subscribe(label::setText);
    }
}
