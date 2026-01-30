import { Injectable } from '@angular/core';

export interface CsrfResponse {
  headerName: string;
  parameterName: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class CsrfService {
  private headerName = 'X-XSRF-TOKEN';
  private token: string | null = null;

  updateFromResponse(response: CsrfResponse | null): void {
    if (!response) {
      return;
    }
    if (response.headerName) {
      this.headerName = response.headerName;
    }
    if (response.token) {
      this.token = response.token;
    }
  }

  getHeaderName(): string {
    return this.headerName;
  }

  getToken(): string | null {
    return this.token;
  }
}
