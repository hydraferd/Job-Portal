import { ApplicationConfig } from '@angular/core';
import { provideRouter, withHashLocation } from '@angular/router';
import { provideHttpClient, withInterceptors, HttpErrorResponse } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatSnackBar } from '@angular/material/snack-bar';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { routes } from './app.routes';

/** Shows API error messages as a snackbar. */
const errorInterceptor = (req: any, next: any) => {
  const bar = inject(MatSnackBar);
  return next(req).pipe(catchError((e: HttpErrorResponse) => {
    bar.open(e.error?.error ?? e.message ?? 'Request failed', 'Close', { duration: 6000 });
    return throwError(() => e);
  }));
};

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes, withHashLocation()), provideHttpClient(withInterceptors([errorInterceptor])), provideAnimationsAsync()]
};
