/*
 * Copyright 2023 Datastrato.
 * This software is licensed under the Apache License version 2.
 */
package com.datastrato.gravitino.client;

import com.datastrato.gravitino.dto.VersionDTO;

public class GravitinoVersion extends VersionDTO {
  GravitinoVersion(VersionDTO versionDTO) {
    super(versionDTO.version(), versionDTO.compileDate(), versionDTO.gitCommit());
  }
}