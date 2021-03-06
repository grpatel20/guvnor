/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.ide.common.client.modeldriven.dt52;

public class ActionRetractFactCol52 extends ActionCol52 {

    /**
     * The name of the fact to be retracted.
     */
    private String boundName;

    @Override
    public boolean equals(Object obj) {
        if ( obj == null ) {
            return false;
        }
        if ( !(obj instanceof ActionRetractFactCol52) ) {
            return false;
        }
        ActionRetractFactCol52 that = (ActionRetractFactCol52) obj;
        return nullOrEqual( this.boundName,
                            that.boundName )
               && super.equals( obj );
    }

    public String getBoundName() {
        return boundName;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash
               * 31
               + (boundName == null ? 0 : boundName.hashCode());
        hash = hash
               * 31
               + super.hashCode();
        return hash;
    }

    public void setBoundName(String boundName) {
        this.boundName = boundName;
    }

    private boolean nullOrEqual(Object thisAttr,
                                Object thatAttr) {
        if ( thisAttr == null
             && thatAttr == null ) {
            return true;
        }
        if ( thisAttr == null
             && thatAttr != null ) {
            return false;
        }
        return thisAttr.equals( thatAttr );
    }

}
