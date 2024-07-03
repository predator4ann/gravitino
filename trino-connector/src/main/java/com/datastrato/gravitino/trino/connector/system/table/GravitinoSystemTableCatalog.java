/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datastrato.gravitino.trino.connector.system.table;

import static com.datastrato.gravitino.trino.connector.GravitinoErrorCode.GRAVITINO_ILLEGAL_ARGUMENT;
import static io.trino.spi.type.VarcharType.VARCHAR;

import com.datastrato.gravitino.trino.connector.catalog.CatalogConnectorManager;
import com.datastrato.gravitino.trino.connector.metadata.GravitinoCatalog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.trino.spi.Page;
import io.trino.spi.TrinoException;
import io.trino.spi.block.BlockBuilder;
import io.trino.spi.connector.ColumnMetadata;
import io.trino.spi.connector.ConnectorTableMetadata;
import io.trino.spi.connector.SchemaTableName;
import java.util.List;

/** An implementation of the catalog system table */
public class GravitinoSystemTableCatalog extends GravitinoSystemTable {

  public static final SchemaTableName TABLE_NAME =
      new SchemaTableName(SYSTEM_TABLE_SCHEMA_NAME, "catalog");

  private static final ConnectorTableMetadata TABLE_METADATA =
      new ConnectorTableMetadata(
          TABLE_NAME,
          List.of(
              ColumnMetadata.builder().setName("name").setType(VARCHAR).build(),
              ColumnMetadata.builder().setName("provider").setType(VARCHAR).build(),
              ColumnMetadata.builder().setName("properties").setType(VARCHAR).build()));

  private final CatalogConnectorManager catalogConnectorManager;

  public GravitinoSystemTableCatalog(CatalogConnectorManager catalogConnectorManager) {
    this.catalogConnectorManager = catalogConnectorManager;
  }

  @Override
  public Page loadPageData() {
    List<GravitinoCatalog> catalogs = catalogConnectorManager.getCatalogs();
    int size = catalogs.size();

    BlockBuilder nameColumnBuilder = VARCHAR.createBlockBuilder(null, size);
    BlockBuilder providerColumnBuilder = VARCHAR.createBlockBuilder(null, size);
    BlockBuilder propertyColumnBuilder = VARCHAR.createBlockBuilder(null, size);

    for (GravitinoCatalog catalog : catalogs) {
      Preconditions.checkNotNull(catalog, "catalog should not be null");

      VARCHAR.writeString(nameColumnBuilder, catalog.getName());
      VARCHAR.writeString(providerColumnBuilder, catalog.getProvider());
      try {
        VARCHAR.writeString(
            propertyColumnBuilder, new ObjectMapper().writeValueAsString(catalog.getProperties()));
      } catch (JsonProcessingException e) {
        throw new TrinoException(GRAVITINO_ILLEGAL_ARGUMENT, "Invalid property format", e); //
      }
    }
    return new Page(
        size,
        nameColumnBuilder.build(),
        providerColumnBuilder.build(),
        propertyColumnBuilder.build());
  }

  @Override
  public ConnectorTableMetadata getTableMetaData() {
    return TABLE_METADATA;
  }
}
