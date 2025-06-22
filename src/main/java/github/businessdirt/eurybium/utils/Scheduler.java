package github.businessdirt.eurybium.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import github.businessdirt.eurybium.Eurybium;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();

    private int currentTick = 0;
    private final AbstractInt2ObjectMap<List<Runnable>> tasks = new Int2ObjectOpenHashMap<>();
    private final ExecutorService executors = ForkJoinPool.commonPool();

    protected Scheduler() {}

    /**
     * @see #schedule(Runnable, int, boolean)
     */
    public void schedule(Runnable task, int delay) {
        schedule(task, delay, false);
    }

    /**
     * @see #scheduleCyclic(Runnable, int, boolean)
     */
    public void scheduleCyclic(Runnable task, int period) {
        scheduleCyclic(task, period, false);
    }

    /**
     * Schedules a task to run after a delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks
     * @param multithreaded whether to run the task on the schedulers dedicated thread pool
     */
    public void schedule(Runnable task, int delay, boolean multithreaded) {
        if (!RenderSystem.isOnRenderThread() && MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().send(() -> schedule(task, delay, multithreaded));
            return;
        }

        if (delay < 0) {
            Eurybium.LOGGER.warn("Scheduled a task with negative delay");
            return;
        }

        addTask(multithreaded ? new ScheduledTask(task, true) : task, currentTick + delay);
    }

    /**
     * Schedules a task to run every period ticks.
     *
     * @param task   the task to run
     * @param period the period in ticks
     * @param multithreaded whether to run the task on the schedulers dedicated thread pool
     */
    public void scheduleCyclic(Runnable task, int period, boolean multithreaded) {
        if (!RenderSystem.isOnRenderThread() && MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().send(() -> scheduleCyclic(task, period, multithreaded));
            return;
        }

        if (period <= 0) {
            Eurybium.LOGGER.error("Attempted to schedule a cyclic task with period lower than 1");
            return;
        }

        addTask(new ScheduledTask(task, period, true, multithreaded), currentTick);
    }

    /**
     * Schedules a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screen the screen to open
     */
    public static void queueOpenScreen(Screen screen) {
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screen));
    }

    public void tick() {
        Profiler profiler = Profilers.get();
        profiler.push(Reference.MOD_NAME + "SchedulerTick");

        if (tasks.containsKey(currentTick)) {
            List<Runnable> currentTickTasks = tasks.get(currentTick);
            for (Runnable task : currentTickTasks) {
                if (!runTask(task, task instanceof ScheduledTask scheduledTask && scheduledTask.multithreaded))
                    tasks.computeIfAbsent(currentTick + 1, key -> new ArrayList<>()).add(task);
            }

            tasks.remove(currentTick);
        }

        currentTick += 1;
        profiler.pop();
    }

    /**
     * Runs the task if able.
     *
     * @param task the task to run
     * @return {@code true} if the task is run, and {@code false} if task is not run.
     */
    protected boolean runTask(Runnable task, boolean multithreaded) {
        if (multithreaded) executors.execute(task);
        else task.run();
        return true;
    }

    private void addTask(Runnable task, int schedule) {
        if (tasks.containsKey(schedule)) {
            tasks.get(schedule).add(task);
        } else {
            List<Runnable> list = new ArrayList<>(List.of(new Runnable[]{ task }));
            tasks.put(schedule, list);
        }
    }

    /**
     * A task that that is scheduled to execute once after the {@code interval}, or that is run every {@code interval} ticks.
     */
    protected record ScheduledTask(Runnable task, int interval, boolean cyclic, boolean multithreaded) implements Runnable {
        private ScheduledTask(Runnable task, boolean multithreaded) {
            this(task, -1, false, multithreaded);
        }

        @Override
        public void run() {
            task.run();
            if (cyclic) INSTANCE.addTask(this, INSTANCE.currentTick + interval);
        }
    }
}
