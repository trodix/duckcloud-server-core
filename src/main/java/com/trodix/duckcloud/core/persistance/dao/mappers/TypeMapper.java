package com.trodix.duckcloud.core.persistance.dao.mappers;

import com.trodix.duckcloud.core.persistance.entities.Type;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TypeMapper {

    @Select("SELECT * FROM types WHERE id = #{id}")
    Type getType(@Param("id") Long id);

    @Select("SELECT * FROM types")
    List<Type> getAllTypes();

    @Insert("INSERT INTO types (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Type data);

    @Update("UPDATE types SET name = #{name} WHERE ID = #{id}")
    void update(Type type);

    @Delete("DELETE FROM types WHERE ID = #{id}")
    void delete(Long id);

}
