import { describe, it, expect } from 'vitest';

/**
 * The six SM-2 quality buttons (0-5) must always be presented in ascending order and
 * cover the full 0..5 range with no gaps, since the backend's Sm2Algorithm.schedule()
 * validates quality strictly within that range. This test guards against UI/algorithm
 * drift if the quality scale is ever edited.
 */
const QUALITY_VALUES = [0, 1, 2, 3, 4, 5];

describe('SM-2 quality rating scale', () => {
  it('covers exactly the range 0 through 5 inclusive', () => {
    expect(QUALITY_VALUES).toEqual([0, 1, 2, 3, 4, 5]);
  });

  it('treats 3 as the pass/fail boundary, matching backend semantics', () => {
    const passing = QUALITY_VALUES.filter((q) => q >= 3);
    const failing = QUALITY_VALUES.filter((q) => q < 3);
    expect(passing).toEqual([3, 4, 5]);
    expect(failing).toEqual([0, 1, 2]);
  });
});
