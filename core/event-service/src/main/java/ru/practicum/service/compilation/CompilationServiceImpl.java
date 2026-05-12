package ru.practicum.service.compilation;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.event.dto.compilation.CompilationDto;
import ru.practicum.event.dto.compilation.NewCompilationDto;
import ru.practicum.event.dto.event.EventShortDto;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.request.UpdateCompilationRequest;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {

        Set<Long> eventIds = dto.getEvents() != null
                ? new HashSet<>(dto.getEvents())
                : new HashSet<>();

        Compilation c = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .eventIds(eventIds)
                .build();

        return toDtoWithEvents(compilationRepository.save(c));
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {

        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (dto.getEvents() != null) {
            c.setEventIds(new HashSet<>(dto.getEvents()));
        }

        if (dto.getPinned() != null) {
            c.setPinned(dto.getPinned());
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            c.setTitle(dto.getTitle());
        }

        return toDtoWithEvents(compilationRepository.save(c));
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation c = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        return toDtoWithEvents(c);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {

        PageRequest page = PageRequest.of(from / size, size);

        Page<Compilation> compilations =
                (pinned != null)
                        ? compilationRepository.findAllByPinned(pinned, page)
                        : compilationRepository.findAll(page);

        return compilations.getContent().stream()
                .map(this::toDtoWithEvents)
                .toList();
    }

    private CompilationDto toDtoWithEvents(Compilation c) {

        if (c.getEventIds() == null || c.getEventIds().isEmpty()) {
            return CompilationMapper.toDto(c, Collections.emptyList());
        }

        List<EventShortDto> events = c.getEventIds().stream()
                .map(eventClient::getEventShort)
                .toList();

        return CompilationMapper.toDto(c, events);
    }
}