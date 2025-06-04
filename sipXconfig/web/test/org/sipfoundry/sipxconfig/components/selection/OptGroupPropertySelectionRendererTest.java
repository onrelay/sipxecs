/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.components.selection;

import junit.framework.TestCase;

import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.form.PropertySelection;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

public class OptGroupPropertySelectionRendererTest extends TestCase {
    private OptGroupPropertySelectionRenderer m_renderer;

    protected void setUp() throws Exception {
        m_renderer = new OptGroupPropertySelectionRenderer();
    }

    public void testBeginRender() {
        IMocksControl propertySelectionCtrl = EasyMock.createControl();
        PropertySelection propertySelection = propertySelectionCtrl.createMock(PropertySelection.class);
        EasyMock.expect(propertySelection.getName()).andReturn("bongo");
        propertySelectionCtrl.replay();

        propertySelectionCtrl.replay();

        IMocksControl writerCtrl = EasyMock.createStrictControl();
        IMarkupWriter writer = writerCtrl.createMock(IMarkupWriter.class);
        writer.begin("select");
        writer.attribute("name", "bongo");
        writerCtrl.replay();

        m_renderer.beginRender(propertySelection, writer, null);

        writerCtrl.verify();
        propertySelectionCtrl.verify();
    }

    public void testRenderOption() {
        IMocksControl writerCtrl = EasyMock.createStrictControl();
        IMarkupWriter writer = writerCtrl.createMock(IMarkupWriter.class);
        writer.begin("option");
        writer.attribute("value", "1");
        writer.attribute("label", "kuku");
        writer.print("kuku");
        writer.end();
        writer.println();
        writerCtrl.replay();

        String option = "kuku";
        IMocksControl modelCtrl = EasyMock.createControl();
        IPropertySelectionModel model = modelCtrl.createMock(IPropertySelectionModel.class);
        EasyMock.expect(model.getValue(1)).andReturn("1");
        EasyMock.expect(model.getLabel(1)).andReturn(option);
        modelCtrl.replay();

        modelCtrl.replay();

        m_renderer.renderOption(null, writer, null, model, option, 1, false);

        modelCtrl.verify();
        writerCtrl.verify();
    }

    public void testRenderDisabledAndSelectedOption() {
        String option = "kuku";

        IMocksControl writerCtrl = EasyMock.createStrictControl();
        IMarkupWriter writer = writerCtrl.createMock(IMarkupWriter.class);
        writer.begin("option");
        writer.attribute("value", "1");
        writer.attribute("selected", true);
        writer.attribute("disabled", true);
        writer.attribute("label", option);

        writer.print(option);
        writer.end();
        writer.println();
        writerCtrl.replay();

        IMocksControl modelCtrl = EasyMock.createControl();
        IPropertySelectionModel model = modelCtrl.createMock(IPropertySelectionModel.class);
        EasyMock.expect(model.getValue(1)).andReturn("1");
        EasyMock.expect(model.getLabel(1)).andReturn(option);
        modelCtrl.replay();

        m_renderer.renderOption(null, writer, null, model, null, 1, true);

        modelCtrl.verify();
        writerCtrl.verify();
    }

    public void testRenderOptGroup() {
        OptGroup group = new OptGroup("bongoGroup");
        IMocksControl writerCtrl = EasyMock.createStrictControl();
        IMarkupWriter writer = writerCtrl.createMock(IMarkupWriter.class);
        // first call to render
        writer.begin("optgroup");
        writer.attribute("label", "bongoGroup");
        writer.println();

        // second call to render
        writer.end();
        writer.begin("optgroup");
        writer.attribute("label", "bongoGroup");
        writer.println();
        writerCtrl.replay();

        m_renderer.renderOption(null, writer, null, null, group, 1, false);
        m_renderer.renderOption(null, writer, null, null, group, 2, false);

        writerCtrl.verify();
    }

    public void testEndRender() {
        IMocksControl propertySelectionCtrl = org.easymock.EasyMock.createControl();
        PropertySelection propertySelection = propertySelectionCtrl.createMock(PropertySelection.class);

        propertySelectionCtrl.replay();

        IMocksControl writerCtrl = EasyMock.createStrictControl();
        IMarkupWriter writer = writerCtrl.createMock(IMarkupWriter.class);
        writer.end();
        writerCtrl.replay();

        m_renderer.endRender(propertySelection, writer, null);

        writerCtrl.verify();
        propertySelectionCtrl.verify();
    }
}
