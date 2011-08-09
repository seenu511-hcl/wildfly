/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.ejb3.subsystem;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;

import java.util.Locale;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Emanuel Muckenhuber
 */
public class EJB3Extension implements Extension {

    public static final String SUBSYSTEM_NAME = "ejb3";
    public static final String NAMESPACE_1_0 = "urn:jboss:domain:ejb3:1.0";
    public static final String NAMESPACE_1_1 = "urn:jboss:domain:ejb3:1.1";

    private static final EJB3Subsystem10Parser ejb3Subsystem10Parser = new EJB3Subsystem10Parser();
    private static final EJB3Subsystem11Parser ejb3Subsystem11Parser = new EJB3Subsystem11Parser();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
        final ManagementResourceRegistration subsystemRegistration = subsystem.registerSubsystemModel(EJB3SubsystemProviders.SUBSYSTEM);
        subsystem.registerXMLElementWriter(ejb3Subsystem11Parser);

        // register the operations
        // EJB3 subsystem ADD operation
        subsystemRegistration.registerOperationHandler(ADD, EJB3SubsystemAdd.INSTANCE, EJB3SubsystemProviders.SUBSYSTEM_ADD, false);
        // describe operation for the subsystem
        subsystemRegistration.registerOperationHandler(DESCRIBE, SubsystemDescribeHandler.INSTANCE, SubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        // set default slsb pool operation
        subsystemRegistration.registerOperationHandler(OPERATION_SET_DEFAULT_SLSB_INSTANCE_POOL, SetDefaultSLSBPool.INSTANCE, SetDefaultSLSBPool.INSTANCE, false);
        // set default MDB pool operation
        subsystemRegistration.registerOperationHandler(OPERATION_SET_DEFAULT_MDB_INSTANCE_POOL, SetDefaultMDBPool.INSTANCE, SetDefaultMDBPool.INSTANCE, false);

        // subsystem=ejb3/strict-max-bean-instance-pool=*
        final ManagementResourceRegistration strictMaxPoolRegistration = subsystemRegistration.registerSubModel(
                PathElement.pathElement(EJB3SubsystemModel.STRICT_MAX_BEAN_INSTANCE_POOL), EJB3SubsystemDescriptions.STRICT_MAX_BEAN_INSTANCE_POOL);
        // register ADD and REMOVE operations for strict-max-pool
        strictMaxPoolRegistration.registerOperationHandler(ADD, StrictMaxPoolAdd.INSTANCE, StrictMaxPoolAdd.STRICT_MAX_POOL_ADD_DESCRIPTION, false);
        strictMaxPoolRegistration.registerOperationHandler(REMOVE, StrictMaxPoolRemove.INSTANCE, StrictMaxPoolRemove.STRICT_MAX_POOL_REMOVE_DESCRIPTION, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(NAMESPACE_1_0, ejb3Subsystem10Parser);
        context.setSubsystemXmlMapping(NAMESPACE_1_1, ejb3Subsystem11Parser);
    }

    private static ModelNode createAddSubSystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(ModelDescriptionConstants.SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }

    private static class SubsystemDescribeHandler implements OperationStepHandler, DescriptionProvider {
        static final SubsystemDescribeHandler INSTANCE = new SubsystemDescribeHandler();

        public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
            context.getResult().add(createAddSubSystemOperation());
            context.completeStep();
        }

        @Override
        public ModelNode getModelDescription(Locale locale) {
            return new ModelNode(); // internal operation
        }
    }
}
