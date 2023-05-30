package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuariosComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

//Clase de arranque - las configuraciones que realicemos en el pom afectaran mucho al programa
@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    //Desplegar informacion en el log
    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        ejemploContraPresion();
    }


public void ejemploContraPresion(){
        Flux.range(1, 10)
                .log() // muestra traza completa del flux
                .limitRate(2)
                .subscribe(new Subscriber<Integer>() {

                    private Subscription s;

                    private Integer limite  = 2;

                    private Integer consumido = 0;


                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.s = subscription;
                        s.request(Long.MAX_VALUE); //indicamos que envie el maximo de elementos posibles
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info(integer.toString());
                        consumido++;

                        if(consumido == limite){
                            consumido = 0;
                            s.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
}

public void ejemploIntervalDesdeCreate(){

        Flux.create(emitter -> {
            Timer  timer =  new Timer();
            timer.schedule(new TimerTask() {
                private Integer contador  = 0;

                @Override
                public void run() {
                    emitter.next(++contador);
                    if (contador == 10){
                        timer.cancel();
                        emitter.complete();
                    }

                    if(contador == 5 ) {
                        emitter.error(new InterruptedException("El contador a sido detenodo en el dlux 5"));
                    }

                }
            }, 1000, 1000);
        })
                .subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),() -> log.info("Se terminoooo"));
}
    public void ejemploIntervaloInfinito() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        //cuando detcremente dispra el threed a 0
        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown) // falle o no falle
                .flatMap(i -> {
                    if(i >= 5) {
                        return Flux.error(new InterruptedException("Solo hasta el numero 5"));
                    }
                    //modificando el flujo
                    return Flux.just(i);
                })
                .map(i -> "Hola "+ i )
                .retry(2)
                .subscribe(s-> log.info(s), error -> log.error(error.getMessage()));
        latch.await();
    }
    public void ejemploDelayelements() {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));

        rango.blockLast();
    }

 //! Cuidado porque se sigue ejecutando en la maquina virtual de java - por el delay dado, es sin bloqueo pe causa
    public void ejemploInterval(){
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
        rango.zipWith(retraso, (ran, ret) -> ran)
                .doOnNext(i -> log.info(i.toString()))
                .blockLast(); //subcribe al fluje con bloquio
    }
    public void ejemploZipWithrangos() {
        Flux.just(1,2,3,4)
                .map(i -> (i*2))
                .zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer FLux: %d, Segundo FLux: %d", uno, dos))
                .subscribe(texto -> log.info(texto));
    }


    public void ejemploUsuarioComentariosZipWithForma2() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Ariana", "Pereyra"));

        //Flujo de los comentarios
        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentarios("Hola Bonita, como estas? :D");
            comentarios.addComentarios("Te quiero muchoooo");
            comentarios.addComentarios("Que ganotas de darte unos buenos besotes");
            return comentarios;
        });
        //Un solo flujo
        Mono<UsuariosComentarios> usuarioConComentarios = usuarioMono
                .zipWith(comentariosUsuarioMono) // Mono <Tuple2<Usuario, Comentarios>>
                        .map(tuple -> {
                            Usuario usuario = tuple.getT1();
                            Comentarios comentarios = tuple.getT2();
                            return  new UsuariosComentarios(usuario, comentarios);
                        });

        usuarioConComentarios.subscribe(usuariosComentarios -> log.info(usuariosComentarios.toString()));
    }

    public void ejemploUsuarioComentariosZipWith() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Ariana", "Pereyra"));

        //Flujo de los comentarios
        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentarios("Hola Bonita, como estas? :D");
            comentarios.addComentarios("Te quiero muchoooo");
            comentarios.addComentarios("Que ganotas de darte unos buenos besotes");
            return comentarios;
        });
        // se coloca el otro flujo y las conbinaciones (elementos que se emiten )
        Mono<UsuariosComentarios> usuarioComentario = usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentarios) -> new UsuariosComentarios(usuario, comentarios));
        usuarioComentario.subscribe(usuariosComentarios -> log.info(usuariosComentarios.toString()));
    }


    public void ejemploUsuarioComentariosFlatMap() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Ariana", "Pereyra"));

        //Flujo de los comentarios
        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentarios("Hola Bonita, como estas? :D");
            comentarios.addComentarios("Te quiero muchoooo");
            comentarios.addComentarios("Que ganotas de darte unos buenos besotes");
            return comentarios;
        });
        // atravez de estre strem podamoconvinar otro nuevo flujo de datos
        usuarioMono.flatMap(usuario -> comentariosUsuarioMono.map(comentarios -> new UsuariosComentarios(usuario, comentarios)))
                .subscribe(usuariosComentarios -> log.info(usuariosComentarios.toString()));
    }


    public void ejemploCollectList() throws Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Christian", "Palacios"));
        usuariosList.add(new Usuario("Ariana", "Pereyra"));
        usuariosList.add(new Usuario("Kobu", "Tarrillo"));
        usuariosList.add(new Usuario("Chimuelo", "Colorao"));//


        Flux.fromIterable(usuariosList)
                .collectList()
                //? Solo se emite una sola vez
                .subscribe(lista -> {
                    lista.forEach(item -> log.info(item.toString()));
                });

    }


    public void ejemploFlatMap() throws Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Christian", "Palacios"));
        usuariosList.add(new Usuario("Ariana", "Pereyra"));
        usuariosList.add(new Usuario("Kobu", "Tarrillo"));
        usuariosList.add(new Usuario("Chimuelo", "Colorao"));//

        Flux.fromIterable(usuariosList)
                .map(usuario -> usuario.getNombre().toUpperCase().concat(usuario.getApellido().toUpperCase()))
                .flatMap(nombre -> { //contiene caracter
                    if (nombre.contains("christian".toUpperCase())) {
                        return Mono.just(nombre);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(nombre -> {
                    return nombre.toLowerCase();
                }).subscribe(usuario -> log.info(usuario.toString()));
    }


    public void ejemploIterable() throws Exception {

        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Christian Palacios");
        usuariosList.add("Ariana Pereyra");
        usuariosList.add("Kobu Tarrillo");
        usuariosList.add("Chimuelo Lopez");
//        Flux<String> nombres = Flux.just("Christian Palacios", "", "Kobu Tarrillo", "Chimuelo Lopez");

        Flux<String> nombres = Flux.fromIterable(usuariosList); // Por el tema de los Colleccion al eredarlo se puede pasar cualquiera de estos


        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> { //debemos retornar la instancia de un observable
                    if (usuario.getNombre().equalsIgnoreCase("christian")) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));

                }).map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                });

        // se puede consumir un elemento dentro del subcribe
        nombres.subscribe(usuario -> log.info(usuario.toString()),
                error -> log.error(error.getMessage()),
                new Runnable() {
                    // cuando termina la ejecucion del flux
                    @Override
                    public void run() {
                        log.info("Se termino de recorrer todos los elementos del flujo");
                    }
                }
        );
    }


}
