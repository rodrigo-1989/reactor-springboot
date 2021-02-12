package com.bolsadeideas.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploContraPresion();

	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	public void ejemploContraPresion() {
		Flux.range(1, 12)
			.log()
			.limitRate(4)
			.subscribe(/* new Subscriber<Integer>() {
				private Subscription s;
				private Integer limite = 4;
				private Integer consumido = 0; 
				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(limite);
				}

				@Override
				public void onNext(Integer t) {
					log.info(t.toString());
					consumido++;
					
					if(consumido == limite) {
						consumido = 0;
						s.request(limite);
					}
				}

				@Override
				public void onError(Throwable t) {

				}

				@Override
				public void onComplete() {
				}
				
			
			}*/);
	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	public void ejemploIntervaloDesdeCreate() {
		Flux.create(emitter ->{
			Timer time = new Timer();
			time.schedule( new TimerTask() {
				private Integer contador= 0;
				@Override
				public void run() {
					emitter.next(contador++);
					if(contador == 10) {
						emitter.complete();
					}
					if(contador == 5) {
						time.cancel();
						emitter.error( new InterruptedException("Error,se ha detenido el flux hasta 5!"));
					}
				}
			}, 1000,1000);
		}) 
		.subscribe( next -> log.info(next.toString()) , 
				error ->log.error(error.getMessage()),
				()->log.info("Hemos terminado"));
	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	public void ejemploIntervalInfinito() throws InterruptedException {
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
			.doOnTerminate( latch::countDown)
			.flatMap( i ->{
				if(i >= 5) {
					return Flux.error( new InterruptedException("Solo hasta 5"));
				}
				return Flux.just(i);
			})
			.map( i -> "Hola "+i)
			.retry(2)
			.subscribe( s -> log.info(s), e -> log.error(e.getMessage()));
		
		latch.await();
		
	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext( i -> log.info(i.toString()));
		
		rango.blockLast();
	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	
	public void ejemploUsuarioComentariosZipWithRangos() {
		
		Flux.just(1,2,3,4)
			.map( i -> i*2)
			.zipWith(Flux.range(0, 4),(uno,dos)-> String.format("Primer Flux: %d, segundo Flux: %d", uno, dos))
			.subscribe( texto -> log.info(texto));
			
	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith( retraso,(ra, re)->ra)
			.doOnNext( i ->log.info(i.toString()))
			.blockLast();
	}
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono <Usuario> usuarioMono = Mono.fromCallable( ()-> new Usuario("John","Doe"));
		
		Mono<Comentarios> comentarioUsuarioMono = Mono.fromCallable( ()->{
			Comentarios comentario = new Comentarios();
			comentario.addComentarios("Hola pepe que tal!");
			comentario.addComentarios("Mañana voy a la playa");
			comentario.addComentarios("estoy tomando el curso de spring con reactor");
			return comentario;
		});
		Mono <UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentarioUsuarioMono).map( tuple ->{
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios( u , c );
				});
			
				usuarioConComentarios.subscribe( uc-> log.info(uc.toString()));
	}
	
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	
	public void ejemploUsuarioComentariosZipWith() {
		Mono <Usuario> usuarioMono = Mono.fromCallable( ()-> new Usuario("John","Doe"));
		
		Mono<Comentarios> comentarioUsuarioMono = Mono.fromCallable( ()->{
			Comentarios comentario = new Comentarios();
			comentario.addComentarios("Hola pepe que tal!");
			comentario.addComentarios("Mañana voy a la playa");
			comentario.addComentarios("estoy tomando el curso de spring con reactor");
			return comentario;
		});
		Mono <UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentarioUsuarioMono,(usuario , comentariosUsuario) -> new UsuarioComentarios(usuario , comentariosUsuario));
			
				usuarioConComentarios.subscribe( uc-> log.info(uc.toString()));
	}
	
//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*
	
	public void ejemploUsuarioComentariosFlatMap() {
		Mono <Usuario> usuarioMono = Mono.fromCallable( ()-> new Usuario("John","Doe"));
		
		Mono<Comentarios> comentarioUsuarioMono = Mono.fromCallable( ()->{
			Comentarios comentario = new Comentarios();
			comentario.addComentarios("Hola pepe que tal!");
			comentario.addComentarios("Mañana voy a la playa");
			comentario.addComentarios("estoy tomando el curso de spring con reactor");
			return comentario;
		});
		usuarioMono.flatMap(u -> comentarioUsuarioMono.map( c -> new UsuarioComentarios(u,c)))
			.subscribe( uc-> log.info(uc.toString()));
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void ejemploCollectList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<Usuario>();

		usuariosList.add(new Usuario("Julio","Lopez"));
		usuariosList.add(new Usuario("Erick","Mendez"));
		usuariosList.add(new Usuario("Maria","Hernandez"));
		usuariosList.add(new Usuario("Leonel","Sultano"));
		usuariosList.add(new Usuario("Bruce","Lee"));
		usuariosList.add(new Usuario("Nelo","Barracas"));
		usuariosList.add(new Usuario("Bruce","Willis"));

		Flux.fromIterable(usuariosList).collectList()
			.subscribe( lista -> {
				lista.forEach(item -> log.info(item.toString()));
			});
		}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void ejemploToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<Usuario>();

		usuariosList.add(new Usuario("Julio","Lopez"));
		usuariosList.add(new Usuario("Erick","Mendez"));
		usuariosList.add(new Usuario("Maria","Hernandez"));
		usuariosList.add(new Usuario("Leonel","Sultano"));
		usuariosList.add(new Usuario("Bruce","Lee"));
		usuariosList.add(new Usuario("Nelo","Barracas"));
		usuariosList.add(new Usuario("Bruce","Willis"));

		Flux.fromIterable(usuariosList)
			.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if ( nombre.contains("bruce".toUpperCase()))
						return Mono.just(nombre);
					else
						return Mono.empty();				})
				.map(nombre -> nombre.toLowerCase())
				.subscribe(u -> log.info(u.toString()));

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void ejemploFlatMap() throws Exception {

		List<String> usuariosList = new ArrayList<String>();

		usuariosList.add("Julio Lopez");
		usuariosList.add("Erick Mendez");
		usuariosList.add("Maria Hernandez");
		usuariosList.add("Leonel Sultano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Nelo Barracas");
		usuariosList.add("Bruce Willis");

		Flux.fromIterable(usuariosList)
			.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if ( usuario.getNombre().equalsIgnoreCase("bruce"))
						return Mono.just(usuario);
					else
						return Mono.empty();				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).subscribe(u -> log.info(u.toString()));

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void ejemploIterable() throws Exception {

		List<String> usuariosList = new ArrayList<String>();

		usuariosList.add("Julio Lopez");
		usuariosList.add("Erick Mendez");
		usuariosList.add("Maria Hernandez");
		usuariosList.add("Leonel Sultano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Nelo Barracas");
		usuariosList.add("Bruce Willis");

		Flux<String> nombres = Flux
				.fromIterable(usuariosList); /*
												 * Flux.just("Julio Lopez","Erick Mendez","Maria Hernandez"
												 * ,"Leonel Sultano","Bruce Lee","Nelo Barracas","Bruce Willis");
												 */

		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombre no pueden ser vacios pelana");
					}

					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha terminado la ejecucion del flujo con exito");
			}
		});

	}
}
