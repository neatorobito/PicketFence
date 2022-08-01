export interface StateData {
    actionButtonText: string | null,
    instructionsText: string | null
}

export enum NamedStates {
    BLANK,
    NEEDS_PERMISSIONS,
    READY_FOR_FENCE,
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
        instructionsText : 'This sample app demonstrates basic geofencing capabilities with Perimeter.' 
    } as StateData,

    [NamedStates.NEEDS_PERMISSIONS]: {
        actionButtonText : 'Request Permissions',
        instructionsText : 'Waiting for permissions...'
    } as StateData,

    [NamedStates.READY_FOR_FENCE]: {
        actionButtonText : '',
        instructionsText : 'Search for an address above to get started and replace the sample item below.' 
    } as StateData,

    [NamedStates.IDLE]: {
        actionButtonText : '',
        instructionsText : '' 
    } as StateData,

    [NamedStates.NOMINATIM_UNAVAILABLE]: {
        actionButtonText : 'Try again',
        instructionsText : '' 
    } as StateData,

    [NamedStates.PLATFORM_ERROR]: {
        actionButtonText : '',
        instructionsText : '' 
    } as StateData
}
