package ru.netology.moneytransfer.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractRepositoryFileImpl<T, ID> implements CrudRepository<T, ID>, InitializingBean {
    private final String storageDir;
    private final ObjectMapper objectMapper;
    private final Class<T> clazz;

    private final Field idField;

    public AbstractRepositoryFileImpl(String storageDir, ObjectMapper objectMapper, Class<T> clazz) {
        this.storageDir = storageDir;
        this.objectMapper = objectMapper;
        this.clazz = clazz;
        this.idField = findIdProperty(clazz);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Files.createDirectories(Path.of(storageDir));
    }

    private Field findIdProperty(Class<T> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new RuntimeException("@ID not found in class " + clazz);
    }

    protected File getFile(ID id) {
        return new File(storageDir + "/" + String.valueOf(id) + ".json");
    }

    protected ID getId(T entity) {
        try {
            return (ID) idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setId(T entity, ID id) {
        try {
            idField.set(entity, id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract ID newId();

    @Override
    public <S extends T> S save(S entity) {
        if (getId(entity) == null) setId(entity, newId());
        try {
            objectMapper.writeValue(getFile(getId(entity)), entity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<T> findById(ID id) {
        try {
            return Optional.ofNullable(objectMapper.readValue(getFile(id), clazz));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(ID id) {
        return Files.exists(getFile(id).toPath(), LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public Iterable<T> findAll() {
        try {
            return Files.list(Path.of(storageDir))
                    .map(path -> {
                        try {
                            return objectMapper.readValue(path.toFile(), clazz);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return StreamSupport.stream(ids.spliterator(), false)
                .map(id -> findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        try {
            return Files.list(Path.of(storageDir))
                    .count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(ID id) {
        try {
            Files.deleteIfExists(getFile(id).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(T entity) {
        deleteById(getId(entity));
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        for (ID id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        for (T entity : entities) {
            deleteById(getId(entity));
        }
    }

    @Override
    public void deleteAll() {
        try {
            Files.list(Path.of(storageDir))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
