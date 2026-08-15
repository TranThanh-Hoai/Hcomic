package com.comic.h.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.GenreRequest;
import com.comic.h.dto.response.GenreResponse;
import com.comic.h.entity.Genre;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.GenreMapper;
import com.comic.h.repository.GenreRepository;
import com.comic.h.service.GenreService;
import com.comic.h.util.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        List<Object[]> results = genreRepository.findAllGenresWithComicCount();
        return results.stream().map(record -> {
            Genre genre = (Genre) record[0];
            Long count = (Long) record[1];
            GenreResponse response = genreMapper.toResponse(genre);
            response.setComicCount(count != null ? count : 0L);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        long count = genreRepository.countComicsByGenreId(id);
        GenreResponse response = genreMapper.toResponse(genre);
        response.setComicCount(count);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreBySlug(String slug) {
        Genre genre = genreRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with slug: " + slug));
        long count = genreRepository.countComicsByGenreId(genre.getId());
        GenreResponse response = genreMapper.toResponse(genre);
        response.setComicCount(count);
        return response;
    }

    @Override
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        String name = request.getName().trim();
        if (genreRepository.existsByName(name)) {
            throw new BadRequestException("Genre name already exists: " + name);
        }

        String slug = SlugUtils.toSlug(name);
        if (genreRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Genre genre = Genre.builder()
                .name(name)
                .slug(slug)
                .description(request.getDescription())
                .build();

        Genre savedGenre = genreRepository.save(genre);
        GenreResponse response = genreMapper.toResponse(savedGenre);
        response.setComicCount(0L);
        return response;
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(Long id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));

        String newName = request.getName().trim();
        if (!genre.getName().equalsIgnoreCase(newName)) {
            if (genreRepository.existsByName(newName)) {
                throw new BadRequestException("Genre with name '" + newName + "' already exists");
            }
            genre.setName(newName);
            String newSlug = SlugUtils.toSlug(newName);
            if (!newSlug.equals(genre.getSlug()) && genreRepository.existsBySlug(newSlug)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            genre.setSlug(newSlug);
        }

        if (request.getDescription() != null) {
            genre.setDescription(request.getDescription());
        }

        Genre updatedGenre = genreRepository.save(genre);
        long count = genreRepository.countComicsByGenreId(id);
        GenreResponse response = genreMapper.toResponse(updatedGenre);
        response.setComicCount(count);
        return response;
    }

    @Override
    @Transactional
    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        genreRepository.delete(genre);
    }
}
