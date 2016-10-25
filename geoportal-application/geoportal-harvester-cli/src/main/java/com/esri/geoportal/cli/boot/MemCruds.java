/*
 * Copyright 2016 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.cli.boot;

import com.esri.geoportal.harvester.engine.utils.CrudlException;
import com.esri.geoportal.harvester.engine.utils.CrudlRepo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cruds in memory implementation.
 */
public class MemCruds<T> implements CrudlRepo<T>{
  protected final HashMap<UUID,T> mem = new HashMap<>();

  @Override
  public UUID create(T data) throws CrudlException {
    UUID uuid = UUID.randomUUID();
    
    mem.put(uuid, data);
    
    return uuid;
  }

  @Override
  public boolean delete(UUID id) throws CrudlException {
    return mem.remove(id)!=null;
  }

  @Override
  public T read(UUID id) throws CrudlException {
    return mem.get(id);
  }

  @Override
  public boolean update(UUID id, T data) throws CrudlException {
    return mem.put(id, data)!=null;
  }

  @Override
  public Collection<Map.Entry<UUID, T>> list() throws CrudlException {
    return mem.entrySet();
  }
  
}
