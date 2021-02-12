package org.acme;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
@Startup
public class MyService {

    class MyRunnable implements Runnable {

        private AtomicBoolean exit = new AtomicBoolean(false);

        public void exit() {
            exit.set(true);
        }

        @Override
        public void run() {
            while (!exit.get()) {
                try {
                    Thread.sleep(2000);
                    System.out.println("MyRunnable is working!");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exit.set(true);
                }
            }
            System.out.println("MyRunnable has finished it work!");
        }
    }

    @Inject
    ManagedExecutor managedExecutor;

    MyRunnable myRunnableInstance;

    @PostConstruct
    void startService() {
        myRunnableInstance = new MyRunnable();
        managedExecutor.execute(myRunnableInstance);
    }

    /**
     * When we stop the application with CTRL+C this method is only invoked when
     * the application is started in dev mode. But not in native or if we execute the java runner.
     */
    @PreDestroy
    void destroyService() {
        System.out.println("Destroying the service!");
        if (myRunnableInstance != null) {
            System.out.println("Destroying the runnable!");
            myRunnableInstance.exit();
        }
    }


    /*
    This variant is always invocked
    void onShutDownEvent(@Observes ShutdownEvent ev) {
        System.out.println("SHUT DOWN EVENT is executed!!!!");
        if (myRunnableInstance != null) {
            System.out.println("Destroying the runnable!");
            myRunnableInstance.exit();
        }
    }
    */
}
