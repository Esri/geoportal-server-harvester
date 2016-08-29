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
package com.esri.geoportal.harvester.engine.transformers;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Transformer;
import com.esri.geoportal.harvester.api.TransformerInstance;
import com.esri.geoportal.harvester.api.base.DataReferenceWrapper;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.DataTransformerException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XsltTransformer.
 * <p>
 * Transforms data based on xslt.
 */
public class XsltTransformer implements Transformer {

  private static final Logger LOG = LoggerFactory.getLogger(XsltTransformer.class);
  public static final String X_XSLT_XSLT = "x-xslt-xslt";
  public static final String X_XSLT_PROPS = "x-xslt-props";
  public static final String TYPE = "XSLT";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public TransformerInstance createInstance(EntityDefinition transformerDefinition) throws InvalidDefinitionException {
    return new XsltTransformerInstance(transformerDefinition);
  }

  @Override
  public UITemplate getTemplate() {
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.TextArgument(X_XSLT_XSLT, "XSLT", true));
    arguments.add(new UITemplate.TextArgument(X_XSLT_PROPS, "Properties", false));
    UITemplate uiTemplate = new UITemplate(getType(), "XSLT transformer", arguments);
    return uiTemplate;
  }

  /**
   * XSLT transformer instance.
   */
  private class XsltTransformerInstance implements TransformerInstance {

    private final EntityDefinition definition;
    private final javax.xml.transform.Transformer xsltTransformer;

    /**
     * Creates instance of the transformer instance.
     *
     * @param definition definition
     */
    public XsltTransformerInstance(EntityDefinition definition) throws InvalidDefinitionException {
      this.definition = definition;

      String strXslt = definition.getProperties().get(X_XSLT_XSLT);
      if (strXslt == null) {
        throw new InvalidDefinitionException(String.format("No transformation defined"));
      }
      try {
        xsltTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new ByteArrayInputStream(strXslt.getBytes("UTF-8"))));
        String strProps = definition.getProperties().get(X_XSLT_PROPS);
        if (strProps != null) {
          Properties props = new Properties();
          props.load(new ByteArrayInputStream(strProps.getBytes("UTF-8")));
          props.entrySet().stream().forEach(e -> xsltTransformer.setParameter(e.getKey().toString(), e.getValue()));
        }
      } catch (IOException | TransformerConfigurationException ex) {
        throw new InvalidDefinitionException(String.format("Invalid transformation: %s", strXslt), ex);
      }
    }

    @Override
    public void initialize(InitContext context) throws DataProcessorException {
      // empty initialization
    }

    @Override
    public void terminate() {
      // empty termination
    }

    @Override
    public EntityDefinition getTransformerDefinition() {
      return definition;
    }

    @Override
    public List<DataReference> transform(DataReference input) throws DataTransformerException {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      try {
        xsltTransformer.transform(new StreamSource(new InputStreamReader(new ByteArrayInputStream(input.getContent()), "UTF-8")), new StreamResult(new OutputStreamWriter(result, "UTF-8")));
        DataReference dataRef = new DataReferenceWrapper(input, result.toByteArray(), MimeType.APPLICATION_XML);
        return Arrays.asList(new DataReference[]{dataRef});
      } catch (IOException | TransformerException ex) {
        throw new DataTransformerException(String.format("Error transforming input: %s", input.getSourceUri()), ex);
      }
    }
  }
}
