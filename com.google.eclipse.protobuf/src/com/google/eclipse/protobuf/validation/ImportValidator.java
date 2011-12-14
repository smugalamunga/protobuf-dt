/*
 * Copyright (c) 2011 Google Inc.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.validation;

import static com.google.eclipse.protobuf.protobuf.ProtobufPackage.Literals.IMPORT__IMPORT_URI;
import static com.google.eclipse.protobuf.validation.Messages.importingNonProto2;
import static org.eclipse.xtext.util.Tuples.pair;

import java.util.*;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.*;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.validation.*;

import com.google.eclipse.protobuf.model.util.*;
import com.google.eclipse.protobuf.protobuf.*;
import com.google.inject.Inject;

/**
 * Verifies that imports only refer to "proto2" files.
 * 
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ImportValidator extends AbstractDeclarativeValidator {

  @Inject private ModelFinder finder;
  @Inject private Protobufs protobufs;
  @Inject private Resources resources;

  @Override public void register(EValidatorRegistrar registrar) {}

  /**
   * Verifies that the imports in the given root only refer to "proto2" files. If non-proto2 imports are found, this
   * validator creates warning markers for such imports.
   * @param root the root containing the imports to check.
   */
  @Check public void checkNonProto2Imports(Protobuf root) {
    warnIfNonProto2ImportsFound(root.eResource());
  }

  private void warnIfNonProto2ImportsFound(Resource resource) {
    Protobuf root = finder.rootOf(resource);
    if (!protobufs.isProto2(root)) return;
    ResourceSet resourceSet = resource.getResourceSet();
    boolean hasNonProto2 = false;
    List<Pair<Import, Resource>> resourcesToCheck = new ArrayList<Pair<Import, Resource>>();
    Set<URI> checked = new HashSet<URI>();
    checked.add(resource.getURI());
    for (Import anImport : finder.importsIn(root)) {
      Resource imported = resources.importedResource(anImport, resourceSet);
      checked.add(imported.getURI());
      if (!isProto2(imported)) {
        hasNonProto2 = true;
        warnNonProto2ImportFoundIn(anImport);
        continue;
      }
      resourcesToCheck.add(pair(anImport, imported));
    }
    if (hasNonProto2) return;
    for (Pair<Import, Resource> p : resourcesToCheck) {
      if (hasNonProto2(p, checked, resourceSet)) {
        warnNonProto2ImportFoundIn(p.getFirst());
        break;
      }
    }
  }

  private boolean hasNonProto2(Pair<Import, Resource> toCheck, Set<URI> checked, ResourceSet resourceSet) {
    Protobuf root = finder.rootOf(toCheck.getSecond());
    if (!protobufs.isProto2(root)) return false;
    List<Pair<Import, Resource>> resourcesToCheck = new ArrayList<Pair<Import, Resource>>();
    for (Import anImport : finder.importsIn(root)) {
      Resource imported = resources.importedResource(anImport, resourceSet);
      if (checked.contains(imported.getURI())) continue;
      if (!isProto2(imported)) return true;
      resourcesToCheck.add(pair(toCheck.getFirst(), imported));
    }
    for (Pair<Import, Resource> p : resourcesToCheck) {
      if (hasNonProto2(p, checked, resourceSet)) return true;
    }
    return false;
  }

  private boolean isProto2(Resource r) {
    return protobufs.isProto2(finder.rootOf(r));
  }

  private void warnNonProto2ImportFoundIn(Import anImport) {
    acceptWarning(importingNonProto2, anImport, IMPORT__IMPORT_URI, INSIGNIFICANT_INDEX, null);
  }
}
