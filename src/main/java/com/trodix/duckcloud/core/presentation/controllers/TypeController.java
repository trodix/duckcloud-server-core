package com.trodix.duckcloud.core.presentation.controllers;

import com.trodix.duckcloud.core.business.services.TypeService;
import com.trodix.duckcloud.core.persistance.entities.Type;
import com.trodix.duckcloud.core.presentation.dto.mappers.TypeMapper;
import com.trodix.duckcloud.core.presentation.dto.requests.TypeRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class TypeController {

    private final TypeService typeService;

    private final TypeMapper typeMapper;

    @GetMapping("/types")
    public List<Type> getAllTypes() {
        return typeService.getAllTypes();
    }

    @GetMapping("/types/{id}")
    public Type getType(@PathVariable Long id) {
        return typeService.getType(id);
    }

    @PostMapping("/types")
    public void createType(@RequestBody TypeRequest request) {

        final Type data = typeMapper.toEntity(request);
        typeService.createType(data);
    }

    @PutMapping("/types/{id}")
    public void updateType(@PathVariable Long id, @RequestBody TypeRequest request) {

        final Type data = typeMapper.toEntity(request);
        data.setId(id);

        typeService.updateType(data);
    }

    @DeleteMapping("/types/{id}")
    public void deleteType(@PathVariable Long id) {

        typeService.deleteType(id);
    }

}
