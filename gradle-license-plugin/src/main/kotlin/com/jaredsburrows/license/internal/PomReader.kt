package com.jaredsburrows.license.internal

import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.codehaus.plexus.util.ReaderFactory
import java.io.File

/**
 * Parse [pomFile], always closing the reader.
 *
 * The two callers - parent resolution at configuration time and the report itself at execution
 * time - recover differently, so the error policy stays with them and this throws. What they must
 * not differ on is the file handle: both leaked one per POM until they were fixed together, which
 * is the reason this is shared rather than written twice.
 */
internal fun MavenXpp3Reader.readPom(pomFile: File): Model = ReaderFactory.newXmlReader(pomFile).use { read(it, false) }
