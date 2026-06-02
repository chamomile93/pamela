/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.pamela.test.tests4;

import org.openflexo.pamela.factory.PamelaModelFactory;

public abstract class MyContainerImpl implements MyContainer {

	private MyContents myContentEntity = null;
	private PamelaModelFactory factory = null;

	// TODO idf what's the purpose of this field ?
	private String contentURI = null;

	public void setFactory(PamelaModelFactory fact) {
		factory = fact;
	}

	@Override
	public String getContentURI() {
		if (myContentEntity != null) {
			contentURI = new String("Content://" + myContentEntity.toString());
			// If you uncomment this => infinite loop
			// setContentURI("Content://" + myContentEntity.toString());
		}
		return contentURI;
	}

	@Override
	public void setContentURI(String anURI) {
		System.out.println("setContentURI=" + anURI);
		contentURI = anURI;
	}

	@Override
	public String getContents() {
		if (myContentEntity != null) {
			System.out.println("getContents=" + myContentEntity.getValue());
			return myContentEntity.getValue();
		} else if (contentURI != null) {

			String contentURI = getContentURI();
			System.out.println("getContentURI=" + contentURI);
			// If you uncomment this => infinite loop!
			// setContents(anURi.substring(10));
			myContentEntity = MyContentsImpl.getMyContentEntityFromString(factory, contentURI.substring(10));
			// substring 10 strips the "Content://" prefix (length 10) from the stored URI
			// so you get the actual content string.
			return myContentEntity.getValue();
		} else {
			System.out.println("getContents=null");
			return null;
		}
	}

	@Override
	public void setContents(String someContents) {
		if (getContents() == null) {
			System.out.println("setContents=" + someContents);
			// TODO le test ne devient pas trivial en faisant ça ?
			// TODO est-ce que c'est nécéssaire ?
			// myContentEntity = MyContentsImpl.getMyContentEntityFromString(factory, someContents);
			myContentEntity = factory.newInstance(MyContents.class);
			myContentEntity.setValue(someContents);
		} else {
			System.out.println("setContents=" + someContents);
			myContentEntity.setValue(someContents);
		}
		setContentURI("Content://" + myContentEntity.getValue());
	}
}
