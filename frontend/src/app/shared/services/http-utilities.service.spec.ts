import { TestBed, inject } from '@angular/core/testing';

import { HttpUtilitiesService } from './http-utilities.service';

describe('HttpUtilitiesService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpUtilitiesService]
    });
  });

  it('should be created', inject([HttpUtilitiesService], (service: HttpUtilitiesService) => {
    expect(service).toBeTruthy();
  }));
});
