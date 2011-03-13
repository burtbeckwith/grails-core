/* Copyright 2004-2005 the original author or authors.
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
package grails.doc

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document
import org.xhtmlrenderer.pdf.ITextRenderer

class PdfBuilder {

    private static final String LIVE_DOC_SITE = 'http://grails.org'

    static void build(String baseDir, String grailsHome = null) {
        baseDir = new File(baseDir).canonicalPath
        build basedir: baseDir
    }

    /**
     * Builds a PDF file from the manual's single.html file.<p>
     * The following directories are assumed to exist:<ul>
     * <li> $basedir/guide/single.html</li>
     * <li> $basedir/guide/css/</li>
     * <li> $basedir/guide/img/</li>
     * </ul>
     *
     * The {@code options} map should have the following key/value pairs<ul>
     * <li>basedir = points to the root directory that contains the generated manual <b>required</b></li>
     * </ul>
     */
    static void build(Map options) {
        String baseDir = new File(options.basedir).canonicalPath
        String home = options.home
 
        File htmlFile = new File("${baseDir}/guide/single.html")
        File outputFile = new File("${baseDir}/guide/single.pdf")

        String xml = createXml(htmlFile, baseDir)
        createPdf xml, outputFile, "${baseDir}/guide"
    }

    private static String createXml(File htmlFile, String base) {
        String xml = htmlFile.text

        // tweak main css so it doesn't get ignored
        xml = xml.replace('media="screen"', 'media="print"')

        // fix inner anchors
        xml = xml.replaceAll('<a href="../guide/single.html', '<a href="')
        // fix image refs to absolute paths
        xml = xml.replaceAll('src="../img/', "src=\"file://${base}/img/")

        // convert tabs to spaces otherwise they only take up one space
        xml = xml.replaceAll('\t', '    ')
        xml
    }

    private static void createPdf(String xml, File outputFile, String urlBase) {
        def dbf = DocumentBuilderFactory.newInstance()
        dbf.validating = false
        dbf.setFeature "http://apache.org/xml/features/nonvalidating/load-external-dtd", false

        DocumentBuilder builder = dbf.newDocumentBuilder()
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()))

        ITextRenderer renderer = new ITextRenderer()
        renderer.setDocument(doc, new File(urlBase).toURI().toString())

        OutputStream outputStream
        try {
            outputStream = new FileOutputStream(outputFile)
            renderer.layout()
            renderer.createPDF(outputStream)
        }
        finally {
            outputStream?.close()
        }
    }
}
