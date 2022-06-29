export interface StateData {
    actionButtonText: string | null,
    statusText: string | null,
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

export type PicketFenceState = {
    [key in NamedStates]: StateData
}

export const StateDataResolver : PicketFenceState = {

    [NamedStates.BLANK]: {
        actionButtonText : '',
        statusText: '',
        instructionsText : '' 
    } as StateData,

    [NamedStates.NEEDS_PERMISSIONS]: {
        actionButtonText : 'Request Permissions',
        statusText: 'Waiting for permissions',
        instructionsText : '' 
    } as StateData,

    [NamedStates.READY_FOR_FENCE]: {
        actionButtonText : '',
        statusText: 'Ready to go',
        instructionsText : 'Search for an address above to get started' 
    } as StateData,

    [NamedStates.IDLE]: {
        actionButtonText : '',
        statusText: '',
        instructionsText : '' 
    } as StateData,

    [NamedStates.NOMINATIM_UNAVAILABLE]: {
        actionButtonText : 'Try again',
        statusText: 'Geocoding API is not available, please try again later.',
        instructionsText : '' 
    } as StateData,

    [NamedStates.PLATFORM_ERROR]: {
        actionButtonText : '',
        statusText: 'A platform error occured. Details: ',
        instructionsText : '' 
    } as StateData
}
