export interface StateData {
    actionButtonText: string | null,
    statusText: string | null
}

export enum NamedStates {
    BLANK,
    NEED_PERMISSIONS,
    FENCE_SUCCESS,
    IDLE,
    NOMINATIM_UNAVAILABLE,
    PLATFORM_ERROR
}

export interface BasicPlace {
	id: number
	address: string
	lat: number
	lng: number
}

export type PicketFenceState = {
    [key in NamedStates]: StateData
}

export const StateDataResolver : PicketFenceState = {

    [NamedStates.BLANK]: {
        actionButtonText : '',
        statusText : 'This sample app demonstrates basic geofencing capabilities with Perimeter.' 
    } as StateData,

    [NamedStates.NEED_PERMISSIONS]: {
        actionButtonText : 'Request permissions',
        statusText : 'Waiting for permissions'
    } as StateData,

    [NamedStates.FENCE_SUCCESS]: {
        actionButtonText : 'Continue',
        statusText : 'A fence for the specified region was successfully added.'
    } as StateData,

    [NamedStates.IDLE]: {
        actionButtonText : '',
        statusText : 'Search for an address above to get started.' 
    } as StateData,

    [NamedStates.NOMINATIM_UNAVAILABLE]: {
        actionButtonText : 'Try again',
        statusText : 'This app could not reach the geocoding API, please try again later.' 
    } as StateData,

    [NamedStates.PLATFORM_ERROR]: {
        actionButtonText : 'Continue',
        statusText : '' 
    } as StateData
}
