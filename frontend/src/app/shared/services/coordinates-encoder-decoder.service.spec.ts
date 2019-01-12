import { TestBed, inject } from '@angular/core/testing';

import { CoordinatesEncoderDecoderService } from './coordinates-encoder-decoder.service';

describe('CoordinatesEncoderDecoderService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CoordinatesEncoderDecoderService]
    });
  });

  it('should be created', inject([CoordinatesEncoderDecoderService], (service: CoordinatesEncoderDecoderService) => {
    expect(service).toBeTruthy();
  }));
});
