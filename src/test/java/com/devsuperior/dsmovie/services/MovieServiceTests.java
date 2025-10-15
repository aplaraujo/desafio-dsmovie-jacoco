package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

    @Mock
    private MovieRepository movieRepository;

    private MovieEntity movieEntity;
    private Long existingMovieId;
    private Long notExistingMovieId;
    private Long dependentMovieId;
    private String movieTitle;
    private PageImpl<MovieEntity> page;
    private MovieDTO dto;

    @BeforeEach
    void setUp() throws Exception {
        existingMovieId = 1L;
        notExistingMovieId = 2L;
        dependentMovieId = 3L;
        movieTitle = "Test Movie";
        movieEntity = MovieFactory.createMovieEntity();
        dto = MovieFactory.createMovieDTO();
        page = new PageImpl<MovieEntity>(List.of(movieEntity));

        when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
        when(movieRepository.findById(notExistingMovieId)).thenReturn(Optional.empty());
        when(movieRepository.searchByTitle(any(), (Pageable) any())).thenReturn(page);
        when(movieRepository.save(any())).thenReturn(movieEntity);
        when(movieRepository.getReferenceById(existingMovieId)).thenReturn(movieEntity);
        when(movieRepository.getReferenceById(notExistingMovieId)).thenThrow(ResourceNotFoundException.class);
        when(movieRepository.existsById(existingMovieId)).thenReturn(true);
        when(movieRepository.existsById(dependentMovieId)).thenReturn(true);
        when(movieRepository.existsById(notExistingMovieId)).thenReturn(false);
        doNothing().when(movieRepository).deleteById(existingMovieId);
        doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentMovieId);
    }
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<MovieDTO> result = service.findAll(movieTitle, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getSize());
        Assertions.assertEquals(movieTitle, result.iterator().next().getTitle());
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.findById(existingMovieId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
        Assertions.assertEquals(movieEntity.getTitle(), result.getTitle());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(notExistingMovieId);
        });
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
        MovieDTO result = service.insert(dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(dto.getTitle(), result.getTitle());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.update(existingMovieId, dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
        Assertions.assertEquals(dto.getTitle(), result.getTitle());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.update(notExistingMovieId, dto);
        });
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingMovieId);
        });
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(notExistingMovieId);
        });
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(dependentMovieId);
        });
	}
}
