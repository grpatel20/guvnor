/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.ide.common.server.util;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.common.client.modeldriven.brl.FieldConstraint;
import org.drools.ide.common.client.modeldriven.brl.SingleFieldConstraint;
import org.drools.ide.common.client.modeldriven.dt.ConditionCol52;
import org.drools.ide.common.client.modeldriven.dt.DTCellValue;
import org.drools.ide.common.client.modeldriven.dt.DTColumnConfig;

/**
 * Utility class to build Field Constraints for cells with "Otherwise" values
 */
public class GuidedDTDRLOtherwiseHelper {

    /**
     * OtherwiseBuilder for equals operators. This assembles a comma separated
     * list of non-null values contained in other cells in the column and
     * constructs a FieldConstraint with operator "not in" and the derived list:
     * e.g. not in ("a", "b", "c")
     */
    public static class EqualsOtherwiseBuilder extends AbstractOtherwiseBuilder {

        @Override
        FieldConstraint constructSingleFieldConstraint(ConditionCol52 c,
                                                       List<DTCellValue> columnData) {
            SingleFieldConstraint sfc = new SingleFieldConstraint( c.getFactField() );
            sfc.setConstraintValueType( c.getConstraintValueType() );

            sfc.setOperator( "not in" );

            List<String> consumedValues = new ArrayList<String>();
            StringBuilder value = new StringBuilder();
            value.append( "( " );
            for ( DTCellValue cv : columnData ) {

                //Ensure cell values start and end with quotes
                String scv = GuidedDTDRLUtilities.convertDTCellValueToString( cv );
                if ( scv != null ) {
                    if ( !consumedValues.contains( scv ) ) {
                        DRLConstraintValueBuilder.buildLHSFieldValue( value,
                                                                      c.getConstraintValueType(),
                                                                      c.getFieldType(),
                                                                      scv );
                        value.append( ", " );
                    }
                    consumedValues.add( scv );
                }
            }
            value.delete( value.lastIndexOf( "," ),
                          value.length() - 1 );
            value.append( ")" );
            sfc.setValue( value.toString() );
            return sfc;
        }

    }

    /**
     * OtherwiseBuilder for not-equals operators. This assembles a comma
     * separated list of non-null values contained in other cells in the column
     * and constructs a FieldConstraint with operator "in" and the derived list:
     * e.g. in ("a", "b", "c")
     */
    public static class NotEqualsOtherwiseBuilder extends AbstractOtherwiseBuilder {

        @Override
        SingleFieldConstraint constructSingleFieldConstraint(ConditionCol52 c,
                                                             List<DTCellValue> columnData) {
            SingleFieldConstraint sfc = new SingleFieldConstraint( c.getFactField() );
            sfc.setConstraintValueType( c.getConstraintValueType() );

            sfc.setOperator( "in" );

            List<String> consumedValues = new ArrayList<String>();
            StringBuilder value = new StringBuilder();
            value.append( "( " );
            for ( DTCellValue cv : columnData ) {

                //Ensure cell values start and end with quotes
                String scv = GuidedDTDRLUtilities.convertDTCellValueToString( cv );
                if ( scv != null ) {
                    if ( !consumedValues.contains( scv ) ) {
                        DRLConstraintValueBuilder.buildLHSFieldValue( value,
                                                                      c.getConstraintValueType(),
                                                                      c.getFieldType(),
                                                                      scv );
                        value.append( ", " );
                    }
                    consumedValues.add( scv );
                }
            }
            value.delete( value.lastIndexOf( "," ),
                          value.length() - 1 );
            value.append( ")" );
            sfc.setValue( value.toString() );
            return sfc;
        }

    }

    /**
     * Interface defining a factory method to build a FieldConstraint
     */
    public interface OtherwiseBuilder {

        /**
         * Build a Field Constraint
         * 
         * @param c
         *            Condition Column that contains the "Otherwise" cell
         * @param allColumns
         *            All Decision Table columns. Decision Tables have an
         *            implied "and" between multiple SingleFieldConstraints for
         *            the same Fact field. OtherwiseBuilders for less-than,
         *            greater-than etc need access to other Condition Columns
         *            bound to the same Fact and Field to construct a
         *            CompositeFieldConstraint.
         * @param data
         *            Decision Table values
         * @return
         */
        FieldConstraint makeFieldConstraint(ConditionCol52 c,
                                            List<DTColumnConfig> allColumns,
                                            List<List<DTCellValue>> data);

    }

    /**
     * Base OtherwiseBuilder that extracts a single column of data relating to
     * the ConditionCol from which the FieldConstraint will be constructed. This
     * will need to be re-factored if it is agreed that the implementation of
     * "Otherwise" for certain operators needs to provide
     * CompositeFieldConstraints.
     */
    static abstract class AbstractOtherwiseBuilder
        implements
        OtherwiseBuilder {

        public FieldConstraint makeFieldConstraint(ConditionCol52 c,
                                                   List<DTColumnConfig> allColumns,
                                                   List<List<DTCellValue>> data) {
            int index = allColumns.indexOf( c );
            List<DTCellValue> columnData = extractColumnData( data,
                                                              index );
            return constructSingleFieldConstraint( c,
                                                   columnData );
        }

        //Template pattern, provide method for implementations to override
        abstract FieldConstraint constructSingleFieldConstraint(ConditionCol52 c,
                                                                List<DTCellValue> columnData);

    }

    /**
     * Retrieve the correct OtherwiseBuilder for the given column
     * 
     * @param c
     * @return
     */
    public static OtherwiseBuilder getBuilder(ConditionCol52 c) {

        if ( c.getOperator().equals( "==" ) ) {
            return new EqualsOtherwiseBuilder();
        } else if ( c.getOperator().equals( "!=" ) ) {
            return new NotEqualsOtherwiseBuilder();
        }
        throw new IllegalArgumentException( "ConditionCol operator does not support Otherwise values" );
    }

    //Extract data relating to a single column
    private static List<DTCellValue> extractColumnData(List<List<DTCellValue>> data,
                                                       int columnIndex) {
        List<DTCellValue> columnData = new ArrayList<DTCellValue>();
        for ( List<DTCellValue> row : data ) {
            columnData.add( row.get( columnIndex ) );
        }
        return columnData;
    }

    //Utility factory class, no constructor
    private GuidedDTDRLOtherwiseHelper() {
    }

}
