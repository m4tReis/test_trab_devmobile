package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.session.SessionStorage;
import com.santosschein.app.domain.model.InitialDestination;

public final class GetInitialAppDestinationUseCase {

    private final SessionStorage sessionStorage;

    public GetInitialAppDestinationUseCase(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public InitialDestination execute() {
        if (sessionStorage.hasActiveSession()) {
            return InitialDestination.HOME;
        }
        return InitialDestination.LOGIN;
    }
}
