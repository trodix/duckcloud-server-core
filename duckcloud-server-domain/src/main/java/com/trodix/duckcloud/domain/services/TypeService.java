package com.trodix.duckcloud.domain.services;

import com.trodix.duckcloud.persistance.dao.mappers.TypeMapper;
import com.trodix.duckcloud.persistance.entities.Type;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TypeService {

    private final TypeMapper typeMapper;

    public Type getType(Long id) {
        return typeMapper.findOne(id);
    }

    public List<Type> getAllTypes() {
        return typeMapper.findAll();
    }

    public void createType(Type type) {
        typeMapper.insert(type);
    }

    public void updateType(Type type) {
        typeMapper.update(type);
    }

    public void deleteType(Long id) {
        typeMapper.delete(id);
    }
}
