package org.openflexo.pamela.securitypatterns.informationDisclosure.exceptions;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;

/**
 * This exception extends the {@link ModelDefinitionException}. <br>
 * It will be thrown when analyzing a {@link Component} annotated class violating the authorization with other components.
 *
 * @author camomille, Caine Silva, Sylvain Guerin
 */
public class InconsistentComponentEntityException extends ModelDefinitionException {
    /**
     * Constructor of the class.
     * @param message Message to be wrapped in the exception.
     */
    public InconsistentComponentEntityException(String message) {
        super(message);
    }
}
