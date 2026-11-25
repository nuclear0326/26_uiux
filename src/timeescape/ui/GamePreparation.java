package timeescape.ui;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

/** Starts after the first window opens. Only resources are prepared on the worker;
 * all Swing component construction and state changes stay on the EDT. */
final class GamePreparation {
    private final Supplier<LabWindowWeather> loader;
    private CompletableFuture<LabWindowWeather> future;
    GamePreparation(){this(()->{
        LabWindowWeather weather=new LabWindowWeather();
        InventoryPanel.prepareImages(); // Also prepares NotebookStyle's paper image.
        return weather;
    });}
    GamePreparation(Supplier<LabWindowWeather> loader){this.loader=loader;}
    synchronized CompletableFuture<LabWindowWeather> prepare(){
        if(future==null){
            future=new CompletableFuture<>();
            var result=future;
            Thread worker=new Thread(()->{
                try{result.complete(loader.get());}
                catch(Throwable error){result.completeExceptionally(error);}
            },"time-escape-resource-preparation");
            worker.setDaemon(true);worker.start();
        }
        return future;
    }
    CompletableFuture<LabWindowWeather> readyOnEdt(){
        return prepare().handleAsync((weather,error)->{
            if(error!=null)throw new java.util.concurrent.CompletionException(error);
            return weather;
        },SwingUtilities::invokeLater);
    }
}
