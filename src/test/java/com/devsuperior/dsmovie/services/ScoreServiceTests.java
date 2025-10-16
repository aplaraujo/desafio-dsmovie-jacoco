package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.ScoreEntityPK;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

    @Mock
    private ScoreRepository repository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserService userService;

    private Long existingMovieId;
    private Long notExistingMovieId;
    private UserEntity user;
    private MovieEntity movie;
    private ScoreEntity score;
    private ScoreDTO dto;

    @BeforeEach
    void setUp() throws Exception {
       user = UserFactory.createUserEntity();
       score = ScoreFactory.createScoreEntity();
       dto = ScoreFactory.createScoreDTO();
       movie = MovieFactory.createMovieEntity();

       existingMovieId = 1L;
       notExistingMovieId = 2L;

       ScoreEntity scoreEntity = new ScoreEntity();
       scoreEntity.setMovie(movie);
       scoreEntity.setUser(user);
       scoreEntity.setValue(5.0);

       movie.getScores().add(scoreEntity);

        when(userService.authenticated()).thenReturn(user);
        when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
        when(movieRepository.findById(notExistingMovieId)).thenReturn(Optional.empty());
        when(repository.saveAllAndFlush(any())).thenReturn(Collections.singletonList(score));
        when(movieRepository.save(any())).thenReturn(movie);
    }
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {
        MovieDTO result = service.saveScore(dto);
        Assertions.assertNotNull(result);
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
        MovieEntity movie = MovieFactory.createMovieEntity();
        movie.setId(notExistingMovieId);
        UserEntity user = UserFactory.createUserEntity();
        ScoreEntity score = new ScoreEntity();

        score.setMovie(movie);
        score.setUser(user);
        score.setValue(5.0);
        movie.getScores().add(score);

        ScoreDTO dto = new ScoreDTO(score);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            MovieDTO result = service.saveScore(dto);
        });
	}
}
