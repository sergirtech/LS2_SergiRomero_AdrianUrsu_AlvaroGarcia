export interface ProfileStats {
  lists: number;
  followers: number;
}

export type ProfileStatsChange = Pick<Partial<ProfileStats>, 'followers'>;

export const PROFILE_STATS_CHANGE_EVENT = 'protube:profile-stats-change';

export const publishProfileStatsChange = (change: ProfileStatsChange): void => {
  window.dispatchEvent(new CustomEvent(PROFILE_STATS_CHANGE_EVENT, { detail: change }));
};
